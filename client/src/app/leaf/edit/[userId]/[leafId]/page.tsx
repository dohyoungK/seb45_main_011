'use client';

import { useForm } from 'react-hook-form';

import { InputValues } from '@/types/common';

import CommonButton from '@/components/common/CommonButton';
import PageTitle from '@/components/common/PageTitle';
import Screws from '@/components/common/Screws';
import TextArea from '@/components/common/TextArea';
import TextInput from '@/components/common/TextInput';
import ImageUpload from '@/components/common/ImageUpload';
import data from '@/mock/leaf.json';

export default function EditLeaf({
  params,
}: {
  params: { userId: string; leafId: string };
}) {
  const { userId, leafId } = params;
  const leaf = data.find((ele) => ele.leafId === parseInt(leafId));
  const {
    register,
    formState: { errors },
    handleSubmit,
    watch,
    clearErrors,
    setValue,
  } = useForm<InputValues>();
  return (
    <div className="flex justify-center items-center">
      <div className="relative w-full max-w-[720px] h-[600px] border-gradient">
        <Screws />
        <div className="p-5 h-full">
          <div className="w-full h-full flex flex-col items-center overflow-y-scroll scrollbar">
            <PageTitle text="식물 카드 수정" className="mb-5" />

            <form
              onSubmit={handleSubmit((data) => console.log(data))}
              className="w-full">
              <div className="w-full flex flex-col items-center gap-2 mb-1">
                <ImageUpload
                  required
                  register={register}
                  watch={watch}
                  errors={errors}
                  clearErrors={clearErrors}
                />
                <div className="w-full flex justify-center gap-2 mb-3">
                  <label className="pt-2 text-xl leading-5 text-brown-80 font-bold">
                    이름 :
                  </label>
                  <TextInput
                    name="plantName"
                    register={register}
                    errors={errors}
                    required
                    setValue={setValue}
                    value={leaf?.leafNickname}
                  />
                </div>
                <div className="w-full flex justify-center gap-2 mb-3">
                  <label className="pt-2 text-xl leading-5 text-brown-80 font-bold">
                    설명 :
                  </label>
                  <TextArea
                    name="leafContent"
                    register={register}
                    errors={errors}
                    required
                    setValue={setValue}
                    value={leaf?.content}
                  />
                </div>
              </div>

              <div className="flex justify-center gap-2">
                <CommonButton usage="submit" size="sm">
                  완료
                </CommonButton>
                <CommonButton usage="button" size="sm">
                  취소
                </CommonButton>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
